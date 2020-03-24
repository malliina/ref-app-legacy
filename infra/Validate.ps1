$templates = Get-ChildItem -File -Name '*.cfn.yml'
foreach ($template in $templates) {
    Write-Output "Validating $template..."
    aws cloudformation validate-template --template-body "file://$template"
}
