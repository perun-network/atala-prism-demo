package perun_network.threshold_ecdsa_demo.frontend

object ServerHandler {
    fun handleHost(): String {
        val templateUrl = this::class.java.classLoader.getResource("html-files/index.html")
            ?: throw IllegalStateException("Template index.html not found")
        val templateContent = templateUrl.openStream().bufferedReader().use { it.readText() }

        return templateContent
    }

    fun handleFaber(): String {
        val templateUrl = this::class.java.classLoader.getResource("html-files/faber.html")
            ?: throw IllegalStateException("Template faber.html not found")
        val templateContent = templateUrl.openStream().bufferedReader().use { it.readText() }

        return templateContent
    }

    fun handleFaberSelect(): String {
        val templateUrl = this::class.java.classLoader.getResource("html-files/faber_select.html")
        ?: throw IllegalStateException("Template select.html not found")
        val templateContent = templateUrl.openStream().bufferedReader().use { it.readText() }
        return templateContent
    }

    fun handleFaberSigning(): String {
        val templateUrl = this::class.java.classLoader.getResource("html-files/faber_sign.html")
            ?: throw IllegalStateException("Template faber_sign.html not found")
        val templateContent = templateUrl.openStream().bufferedReader().use { it.readText() }

        return templateContent
    }

    fun handleAlice(): String {
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

        val templateUrl = this::class.java.classLoader.getResource("html-files/alice.html")
            ?: throw IllegalStateException("Template alice.html not found")
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
        // Load the alice.html template from the classpath.
        val templateUrl = this::class.java.classLoader.getResource("html-files/import.html")
            ?: throw IllegalStateException("Template import.html not found")

        val templateContent = templateUrl.openStream().bufferedReader().use { it.readText() }

        return templateContent
    }

    fun handleVerify(): String {
        val templateUrl = this::class.java.classLoader.getResource("html-files/verify.html")
            ?: throw IllegalStateException("Template verify.html not found")

        val templateContent = templateUrl.openStream().bufferedReader().use { it.readText() }

        return templateContent
    }

}