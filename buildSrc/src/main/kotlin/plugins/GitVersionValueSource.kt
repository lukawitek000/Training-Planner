package plugins

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

interface GitCommitCountParameters : ValueSourceParameters {
    val rootDir: DirectoryProperty
}

abstract class GitCommitCountValueSource : ValueSource<Int, GitCommitCountParameters> {
    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): Int {
        val output = ByteArrayOutputStream()
        return try {
            val root = parameters.rootDir.asFile.orNull
            val result = execOperations.exec {
                if (root != null) {
                    workingDir = root
                }
                isIgnoreExitValue = true
                commandLine("git", "rev-list", "--count", "HEAD")
                standardOutput = output
                errorOutput = ByteArrayOutputStream()
            }
            if (result.exitValue == 0) {
                output.toString().trim().toIntOrNull() ?: 1
            } else {
                1
            }
        } catch (_: Exception) {
            1
        }
    }
}
