package com.vomiter.gradle

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy

class LangMergePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // 1) 建 extension
        final LangMergeExtension ext =
                project.extensions.create('langMerge', LangMergeExtension, project)

        // 2) 註冊 mergeLang 任務
        def mergeTask = project.tasks.register('mergeLang') { task ->
            task.group = 'lang'
            task.description = 'Merge generated lang JSON with manual lang JSON (manual overrides).'

            task.onlyIf {
                ext.manualLangDir.get().asFile.exists() ||
                        ext.generatedLangDir.get().asFile.exists()
            }

            // 必須先變成 File
            def manualDirFile    = ext.manualLangDir.get().asFile
            def generatedDirFile = ext.generatedLangDir.get().asFile

            def manualTree = project.fileTree(
                    dir: manualDirFile,
                    includes: ['**/*.json']
            )
            def generatedTree = project.fileTree(
                    dir: generatedDirFile,
                    includes: ['**/*.json']
            )

            task.inputs.files(manualTree, generatedTree)
            task.outputs.dir(ext.outputDir)

            task.doLast {
                String modId  = ext.modId.get()
                File genDir   = ext.generatedLangDir.get().asFile
                File manDir   = ext.manualLangDir.get().asFile
                File outRoot  = ext.outputDir.get().asFile

                // 收集所有語系檔名
                Set<String> localeNames = [] as Set
                if (genDir.exists()) {
                    genDir.eachFileMatch(~/.*\.json/) { f ->
                        localeNames.add(f.name)
                    }
                }
                if (manDir.exists()) {
                    manDir.eachFileMatch(~/.*\.json/) { f ->
                        localeNames.add(f.name)
                    }
                }

                def slurper = new JsonSlurper()

                localeNames.toList().sort().each { String locName ->
                    File outFile = new File(outRoot, "assets/${modId}/lang/${locName}")
                    outFile.parentFile.mkdirs()

                    Map merged = [:]

                    File genFile = new File(genDir, locName)
                    File manFile = new File(manDir, locName)

                    // 1) 先 generated
                    if (genFile.exists()) {
                        merged.putAll((Map) slurper.parse(genFile))
                    }
                    // 2) 再 manual 覆蓋
                    if (manFile.exists()) {
                        merged.putAll((Map) slurper.parse(manFile))
                    }

                    outFile.text = JsonOutput.prettyPrint(JsonOutput.toJson(merged))
                }
            }
        }

        // 3) 把 mergeLang 接到 processResources
        project.plugins.withId('java') {
            project.tasks.named('processResources', Copy).configure { Copy pr ->
                pr.dependsOn(mergeTask)

                // 原始資源：排除 lang
                pr.from(project.layout.projectDirectory.dir('src/main/resources')) {
                    exclude("assets/${ext.modId.get()}/lang/*.json")
                }
                // datagen 資源：排除 lang
                pr.from(project.layout.projectDirectory.dir('src/generated/resources')) {
                    exclude("assets/${ext.modId.get()}/lang/*.json")
                }
                // 合併結果：塞回去
                pr.from(ext.outputDir) {
                    include("assets/${ext.modId.get()}/lang/*.json")
                }

                pr.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE)
            }
        }
    }

    /**
     * extension
     */
    static class LangMergeExtension {
        final Property<String> modId
        final DirectoryProperty generatedLangDir
        final DirectoryProperty manualLangDir
        final DirectoryProperty outputDir

        LangMergeExtension(Project project) {
            String defaultModId = (project.findProperty('mod_id') ?: 'unknown_mod').toString()

            this.modId = project.objects.property(String)
            this.modId.convention(defaultModId)

            this.generatedLangDir = project.objects.directoryProperty()
            this.manualLangDir    = project.objects.directoryProperty()
            this.outputDir        = project.objects.directoryProperty()

            this.generatedLangDir.convention(
                    project.layout.projectDirectory.dir("src/generated/resources/assets/${defaultModId}/lang")
            )
            this.manualLangDir.convention(
                    project.layout.projectDirectory.dir("src/main/resources/assets/${defaultModId}/lang")
            )
            this.outputDir.convention(
                    project.layout.buildDirectory.dir("mergedResources")
            )
        }
    }
}
