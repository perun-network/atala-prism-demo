package perun_network_threshold_ecdsa.frontend

object ServerHandler {
    fun handleHost(): String {
        val templateUrl = this::class.java.classLoader.getResource("html-files/index.html")
            ?: throw IllegalStateException("Template index.html not found")
        val templateContent = templateUrl.openStream().bufferedReader().use { it.readText() }

        return templateContent
    }

    fun handleSigner(): String {
        val templateUrl = this::class.java.classLoader.getResource("html-files/signer.html")
            ?: throw IllegalStateException("Template signer.html not found")
        val templateContent = templateUrl.openStream().bufferedReader().use { it.readText() }

        return templateContent
    }

    fun handleWallet(): String {
        // Create an instance of PageData with the desired values.
        val pageData = PageData(
            name = "ALICE",
            familyName = "SMITH",
            gender = "Female",
            residentSince = "2015-01-01",
            lprCategory = "C09",
            lprNumber = "999-999-999",
            commuterClassification = "C1",
            nationality = "Bahamas",
            birthDate = "1999-07-17"
        )

        // Load the wallet.html template from the classpath.
        val templateUrl = this::class.java.classLoader.getResource("html-files/wallet.html")
            ?: throw IllegalStateException("Template wallet.html not found")
        val templateContent = templateUrl.openStream().bufferedReader().use { it.readText() }

        // Replace placeholders in the template with data from pageData.
        val rendered = templateContent
            .replace("{{.Name}}", pageData.name)
            .replace("{{.FamilyName}}", pageData.familyName)
            .replace("{{.Gender}}", pageData.gender)
            .replace("{{.ResidentSince}}", pageData.residentSince)
            .replace("{{.LprCategory}}", pageData.lprCategory)
            .replace("{{.LprNumber}}", pageData.lprNumber)
            .replace("{{.CommuterClassification}}", pageData.commuterClassification)
            .replace("{{.Nationality}}", pageData.nationality)
            .replace("{{.BirthDate}}", pageData.birthDate)

        return rendered
    }

    fun handleImport(): String {
        // Load the wallet.html template from the classpath.
        val templateUrl = this::class.java.classLoader.getResource("html-files/import.html")
            ?: throw IllegalStateException("Template import.html not found")

        val templateContent = templateUrl.openStream().bufferedReader().use { it.readText() }

        return templateContent
    }

    fun handleVerify(): String {
        // Load the wallet.html template from the classpath.
        val templateUrl = this::class.java.classLoader.getResource("html-files/export.html")
            ?: throw IllegalStateException("Template import.html not found")

        val templateContent = templateUrl.openStream().bufferedReader().use { it.readText() }

        return templateContent
    }

}