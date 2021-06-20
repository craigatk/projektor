package projektor.server.example.coverage

class CoberturaXmlLoader : CoverageXmlLoader() {

    fun nodeScriptCobertura() = loadTextFromFile("cobertura/node-script-cobertura.xml")
    fun uiCobertura() = loadTextFromFile("cobertura/ui-cobertura.xml")
}
