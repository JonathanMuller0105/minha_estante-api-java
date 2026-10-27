<#
.SYNOPSIS
Executa os dez cenários por HTTP real e compara a API com consultas H2 de leitura.
.DESCRIPTION
Cliente alternativo ao Postman: não executa os scripts pm.* nem comprova importação
ou execução no aplicativo. Reinicie o servidor antes de executar para obter H2 vazio.
As collections são a fonte dos métodos, URLs e corpos; IDs vêm das respostas.
Requer PowerShell 7 e a aplicação já iniciada. Não altera arquivos Java nem usa SQL
de escrita. Evidências incluem corpos, códigos, Location e resultados SQL observados.
#>
[CmdletBinding()]
param(
	# Executa os três cadastros confirmados pelo aluno somente após os dez obrigatórios.
	[switch]$IncluirDemonstracaoPessoal
)

$ErrorActionPreference = 'Stop'
$collection = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'Minha Estante API.postman_collection.json') -Raw | ConvertFrom-Json
$baseUrl = ($collection.variable | Where-Object key -eq 'baseUrl').value
if ($baseUrl -ne 'http://localhost:8080') { throw 'Este roteiro valida somente localhost:8080.' }
$evidencia = [ordered]@{
	inicio = [DateTimeOffset]::Now.ToString('o')
	cliente = 'PowerShell 7 / Invoke-WebRequest; nao e execucao no Postman'
	baseUrl = $baseUrl
	postman = 'PENDENTE: importar e executar as collections no aplicativo'
	obrigatorios = @()
	demonstracaoPessoal = @()
	h2 = @()
	resultado = 'EM EXECUCAO'
}
$variaveis = @{ baseUrl = $baseUrl }

# Registra cada verificação para distinguir evidência observada de resultado esperado.
function Confirmar([bool]$Condicao, [string]$Descricao) {
	$script:validacoesAtuais += [ordered]@{ descricao = $Descricao; aprovado = $Condicao }
	if (!$Condicao) { throw $Descricao }
}

# Confere os quatro atributos sem depender da ordem das propriedades do JSON.
function Confirmar-Livro($Livro, $Esperado) {
	Confirmar ($null -ne $Livro) 'Objeto de livro presente'
	Confirmar ($Livro.id -eq $Esperado.id) 'ID preservado'
	Confirmar ($Livro.titulo -ceq $Esperado.titulo) 'Titulo corresponde ao esperado'
	Confirmar ($Livro.autor -ceq $Esperado.autor) 'Autor corresponde ao esperado'
	Confirmar ($Livro.lido -eq $Esperado.lido) 'Estado lido corresponde ao esperado'
}

# Decodifica apenas a tabela de resultado SQL devolvida pelo console H2.
function Ler-TabelaH2([string]$Html) {
	$tabela = [regex]::Match($Html, '(?s)<table[^>]*class="result(?:Set)?"[^>]*>(.*?)</table>')
	if (!$tabela.Success) { throw 'Tabela de resultado SQL nao encontrada no console H2.' }
	$linhas = @([regex]::Matches($tabela.Groups[1].Value, '(?s)<tr[^>]*>(.*?)</tr>'))
	$nomes = @()
	$registros = @()
	foreach ($linha in $linhas) {
		$valores = @([regex]::Matches($linha.Groups[1].Value, '(?s)<t[dh][^>]*>(.*?)</t[dh]>') | ForEach-Object {
			[System.Net.WebUtility]::HtmlDecode(($_.Groups[1].Value -replace '<[^>]+>', '')).Trim()
		})
		if ($nomes.Count -eq 0) { $nomes = $valores; continue }
		if ($valores.Count -ne $nomes.Count) { throw 'Quantidade inesperada de colunas SQL.' }
		$registro = [ordered]@{}
		for ($i = 0; $i -lt $nomes.Count; $i++) { $registro[$nomes[$i]] = $valores[$i] }
		$registros += [pscustomobject]$registro
	}
	return [pscustomobject]@{ colunas = $nomes; registros = $registros }
}

# Usa somente os quatro SELECTs exigidos. A sessão do console não é salva em disco.
function Conferir-H2([string]$Etapa, [object[]]$Esperados) {
	$console = "$baseUrl/h2-console/"
	$abertura = Invoke-WebRequest -Uri $console -SessionVariable sessaoH2 -TimeoutSec 30
	$idSessao = [regex]::Match($abertura.Content, 'jsessionid=([a-f0-9]+)').Groups[1].Value
	if (!$idSessao) { throw 'Sessao do console H2 nao encontrada.' }
	$login = Invoke-WebRequest -Uri ($console + 'login.do?jsessionid=' + $idSessao) -WebSession $sessaoH2 -Method Post -Body @{
		driver = 'org.h2.Driver'; url = 'jdbc:h2:mem:estante'; user = 'sa'; password = ''
	} -TimeoutSec 30
	if ($login.Content -notmatch '<frameset') { throw 'Conexao do H2 nao confirmada.' }
	$consultas = @('SELECT * FROM LIVROS;', 'SELECT ID, TITULO, AUTOR, LIDO FROM LIVROS;', 'SELECT * FROM LIVROS WHERE LIDO = TRUE;', 'SELECT COUNT(*) FROM LIVROS;')
	$captura = [ordered]@{ etapa = $Etapa; instante = [DateTimeOffset]::Now.ToString('o'); consoleHttp = [int]$abertura.StatusCode; jdbcUrl = 'jdbc:h2:mem:estante'; consultas = @(); aprovado = $false }
	$evidencia.h2 += $captura
	for ($q = 0; $q -lt $consultas.Count; $q++) {
		$resposta = Invoke-WebRequest -Uri ($console + 'query.do?jsessionid=' + $idSessao) -WebSession $sessaoH2 -Method Post -Body @{ sql = $consultas[$q] } -TimeoutSec 30
		$tabela = Ler-TabelaH2 $resposta.Content
		$captura.consultas += [ordered]@{ sql = $consultas[$q]; http = [int]$resposta.StatusCode; colunas = $tabela.colunas; registros = $tabela.registros }
		if ($q -eq 3) {
			if ($tabela.registros.Count -ne 1 -or [int]$tabela.registros[0].'COUNT(*)' -ne $Esperados.Count) { throw 'Contagem H2 difere da API.' }
			continue
		}
		$comparar = if ($q -eq 2) { @($Esperados | Where-Object lido -eq $true) } else { @($Esperados) }
		if ($tabela.registros.Count -ne $comparar.Count) { throw 'Quantidade de registros H2 difere da API.' }
		foreach ($esperado in $comparar) {
			$linha = @($tabela.registros | Where-Object { $_.ID -eq [string]$esperado.id })
			if ($linha.Count -ne 1 -or $linha[0].TITULO -cne $esperado.titulo -or $linha[0].AUTOR -cne $esperado.autor -or $linha[0].LIDO.ToLowerInvariant() -ne ([string]$esperado.lido).ToLowerInvariant()) { throw 'Campos H2 diferem da resposta JSON.' }
		}
	}
	$captura.aprovado = $true
	Write-Output ("H2: $Etapa; quatro SELECTs aprovados; registros: " + $Esperados.Count)
}

try {
	$esperadosHttp = @(200, 201, 201, 200, 200, 200, 200, 400, 204, 404)
	for ($i = 0; $i -lt $collection.item.Count; $i++) {
		$item = $collection.item[$i]
		$url = [string]$item.request.url
		foreach ($chave in $variaveis.Keys) { $url = $url.Replace('{{' + $chave + '}}', [string]$variaveis[$chave]) }
		if ($url -match '{{') { throw 'Variavel de URL nao resolvida.' }
		$parametros = @{ Uri = $url; Method = $item.request.method; TimeoutSec = 30; SkipHttpErrorCheck = $true; Headers = @{ Accept = 'application/json' } }
		if ($item.request.body) { $parametros.ContentType = 'application/json'; $parametros.Body = [System.Text.Encoding]::UTF8.GetBytes($item.request.body.raw) }
		$resposta = Invoke-WebRequest @parametros
		$script:validacoesAtuais = @()
		$registro = [ordered]@{ numero = $i + 1; nome = $item.name; instante = [DateTimeOffset]::Now.ToString('o'); metodo = $item.request.method; url = $url; corpoEnviado = $item.request.body.raw; esperadoHttp = $esperadosHttp[$i]; observadoHttp = [int]$resposta.StatusCode; location = [string]$resposta.Headers.Location; contentType = [string]$resposta.Headers.'Content-Type'; corpoRecebido = [string]$resposta.Content; validacoes = @(); aprovado = $false }
		$evidencia.obrigatorios += $registro
		try {
			Confirmar ($resposta.StatusCode -eq $esperadosHttp[$i]) 'Codigo HTTP esperado'
			$dados = if ($i -ne 8) { ConvertFrom-Json -InputObject $resposta.Content -NoEnumerate } else { $null }
			if ($i -ne 8) { Confirmar ([string]$resposta.Headers.'Content-Type' -match 'application/json') 'Resposta JSON' }
			switch ($i + 1) {
				1 { Confirmar ($dados -is [array] -and $dados.Count -eq 0) 'Banco vazio antes do primeiro cadastro' }
				2 {
					Confirmar ($dados.id -gt 0 -and ($dados.id -is [int] -or $dados.id -is [long])) 'ID gerado numerico positivo'
					$variaveis.livroId = $dados.id
					$primeiro = $dados
					Confirmar-Livro $dados ([pscustomobject]@{ id = $dados.id; titulo = 'O Hobbit'; autor = 'J. R. R. Tolkien'; lido = $false })
					Confirmar ([string]$resposta.Headers.Location -eq "$baseUrl/api/livros/$($dados.id)") 'Location do primeiro cadastro'
				}
				3 {
					Confirmar ($dados.id -gt 0 -and $dados.id -ne $primeiro.id) 'Segundo ID valido e distinto'
					$variaveis.segundoLivroId = $dados.id
					$segundo = $dados
					Confirmar-Livro $dados ([pscustomobject]@{ id = $dados.id; titulo = 'Dom Casmurro'; autor = 'Machado de Assis'; lido = $false })
					Confirmar ([string]$resposta.Headers.Location -eq "$baseUrl/api/livros/$($dados.id)") 'Location do segundo cadastro'
				}
				4 {
					Confirmar ($dados -is [array] -and $dados.Count -eq 2) 'Lista contem exatamente dois livros'
					Confirmar-Livro (@($dados | Where-Object id -eq $primeiro.id)[0]) $primeiro
					Confirmar-Livro (@($dados | Where-Object id -eq $segundo.id)[0]) $segundo
				}
				5 { Confirmar-Livro $dados $primeiro }
				6 {
					Confirmar-Livro $dados ([pscustomobject]@{ id = $primeiro.id; titulo = 'O Hobbit - Edicao revista'; autor = $primeiro.autor; lido = $false })
					$atualizado = $dados
				}
				7 {
					Confirmar-Livro $dados ([pscustomobject]@{ id = $atualizado.id; titulo = $atualizado.titulo; autor = $atualizado.autor; lido = $true })
					Confirmar (!$item.request.body) 'PATCH enviado sem corpo JSON'
					Conferir-H2 'apos teste 7' @($dados, $segundo)
				}
				8 { Confirmar ($dados.erro -ceq 'Informe titulo e autor validos.') 'Erro de validacao presente e correto' }
				9 { Confirmar ([string]$resposta.Content -ceq '') 'DELETE sem corpo de resposta' }
				10 {
					Confirmar ($dados.erro -ceq "Livro com id $($primeiro.id) nao encontrado.") 'Erro 404 identifica o livro excluido'
					Conferir-H2 'apos teste 10' @($segundo)
				}
			}
			$registro.aprovado = $true
		} finally { $registro.validacoes = $script:validacoesAtuais }
		Write-Output ("Teste $($i + 1): HTTP " + $resposta.StatusCode + ' aprovado')
	}
	if ($IncluirDemonstracaoPessoal) {
		$demo = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'Demonstracao pessoal.postman_collection.json') -Raw | ConvertFrom-Json
		$pessoais = @()
		for ($n = 1; $n -le 3; $n++) {
			$titulo = ($demo.variable | Where-Object key -eq "livroPessoal${n}Titulo").value
			$autor = ($demo.variable | Where-Object key -eq "livroPessoal${n}Autor").value
			if ([string]::IsNullOrWhiteSpace($titulo) -or [string]::IsNullOrWhiteSpace($autor)) { throw 'Escolhas pessoais ainda nao preenchidas.' }
			$corpo = @{ titulo = $titulo; autor = $autor; lido = $false } | ConvertTo-Json
			$resposta = Invoke-WebRequest -Uri "$baseUrl/api/livros" -Method Post -ContentType 'application/json' -Body ([System.Text.Encoding]::UTF8.GetBytes($corpo)) -SkipHttpErrorCheck -TimeoutSec 30
			$script:validacoesAtuais = @()
			$registro = [ordered]@{ titulo = $titulo; autor = $autor; metodo = 'POST'; url = "$baseUrl/api/livros"; observadoHttp = [int]$resposta.StatusCode; corpoEnviado = $corpo; corpoRecebido = [string]$resposta.Content; location = [string]$resposta.Headers.Location; validacoes = @(); aprovado = $false }
			$evidencia.demonstracaoPessoal += $registro
			try {
				Confirmar ($resposta.StatusCode -eq 201) 'Cadastro pessoal retorna 201'
				$dados = $resposta.Content | ConvertFrom-Json
				Confirmar ($dados.id -gt 0 -and $dados.id -notin @($pessoais.id) -and $dados.id -ne $segundo.id) 'ID pessoal valido e distinto'
				Confirmar-Livro $dados ([pscustomobject]@{ id = $dados.id; titulo = $titulo; autor = $autor; lido = $false })
				Confirmar ([string]$resposta.Headers.Location -eq "$baseUrl/api/livros/$($dados.id)") 'Location do livro pessoal'
				$pessoais += $dados
				$registro.aprovado = $true
			} finally { $registro.validacoes = $script:validacoesAtuais }
		}
		Conferir-H2 'apos demonstracao pessoal' (@($segundo) + $pessoais)
	}
	$evidencia.resultado = 'APROVADO POR HTTP E H2; POSTMAN PENDENTE'
} catch {
	$evidencia.resultado = 'FALHA'
	$evidencia.erro = $_.Exception.Message
	throw
} finally {
	$evidencia.fim = [DateTimeOffset]::Now.ToString('o')
	$destino = Join-Path $PSScriptRoot 'evidencias/http-e-h2.json'
	$evidencia | ConvertTo-Json -Depth 40 | Set-Content -LiteralPath $destino -Encoding utf8
	Write-Output ('Evidencias gravadas em ' + $destino)
}
