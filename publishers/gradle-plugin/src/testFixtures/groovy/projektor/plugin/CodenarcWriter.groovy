package projektor.plugin

import org.junit.rules.TemporaryFolder

class CodenarcWriter {
    static File writeCodenarcConfigFile(TemporaryFolder projectDir) {
        File configDirectory = projectDir.newFolder("config", "codenarc")

        File configFile = new File(configDirectory, "codenarc.xml")
        configFile.createNewFile()

        configFile.text = """
<ruleset xmlns="http://codenarc.org/ruleset/1.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://codenarc.org/ruleset/1.0 http://codenarc.org/ruleset-schema.xsd"
         xsi:noNamespaceSchemaLocation="http://codenarc.org/ruleset-schema.xsd">

    <description>Sample rule set</description>

    <ruleset-ref path="rulesets/basic.xml"/>
    <ruleset-ref path="rulesets/concurrency.xml"/>
    <ruleset-ref path="rulesets/convention.xml"/>
    <ruleset-ref path="rulesets/design.xml"/>
    <ruleset-ref path="rulesets/dry.xml">
    </ruleset-ref>
    <ruleset-ref path="rulesets/exceptions.xml"/>
    <ruleset-ref path="rulesets/formatting.xml"/>
    <ruleset-ref path="rulesets/generic.xml"/>
    <ruleset-ref path="rulesets/grails.xml"/>
    <ruleset-ref path="rulesets/groovyism.xml"/>
    <ruleset-ref path="rulesets/imports.xml">
        <exclude name="MisorderedStaticImports"/>
    </ruleset-ref>
    <ruleset-ref path="rulesets/jdbc.xml"/>
    <ruleset-ref path="rulesets/junit.xml"/>
    <ruleset-ref path="rulesets/logging.xml"/>
    <ruleset-ref path="rulesets/naming.xml">
        <rule-config name="FieldName">
            <property name="ignoreFieldNames" value="log"/>
        </rule-config>
        <rule-config name="MethodName">
            <property name="doNotApplyToClassNames" value="*Spec"/>
        </rule-config>
        <exclude name="FactoryMethodName"/>
    </ruleset-ref>
    <ruleset-ref path="rulesets/security.xml">
        <exclude name="JavaIoPackageAccess"/>
    </ruleset-ref>
    <ruleset-ref path="rulesets/serialization.xml"/>
    <ruleset-ref path="rulesets/size.xml"/>
    <ruleset-ref path="rulesets/unnecessary.xml">
        <exclude name="UnnecessaryPackageReference"/>
    </ruleset-ref>
    <ruleset-ref path="rulesets/unused.xml"/>

</ruleset>
"""

        return configFile
    }
}
