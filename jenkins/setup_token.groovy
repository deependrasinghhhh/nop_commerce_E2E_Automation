import jenkins.model.Jenkins
import hudson.model.User
import jenkins.security.ApiTokenProperty
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import hudson.util.Secret

println("=== RUNNING SETUP TOKEN & CREDENTIALS SCRIPT ===")

// 1. Ensure required Jenkins credentials exist
try {
    def provider = SystemCredentialsProvider.getInstance()
    def store = provider.getStore()

    def addSecretIfMissing = { String id, String secretValue, String description ->
        def existing = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
            com.cloudbees.plugins.credentials.Credentials.class,
            Jenkins.instance,
            null,
            null
        ).find { it.id == id }

        if (existing == null) {
            def cred = new StringCredentialsImpl(
                com.cloudbees.plugins.credentials.CredentialsScope.GLOBAL,
                id,
                description,
                Secret.fromString(secretValue)
            )
            store.addCredentials(Domain.global(), cred)
            println("Created Jenkins credential: " + id)
        } else {
            println("Jenkins credential already present: " + id)
        }
    }

    addSecretIfMissing("nop-admin-email", "admin@yourstore.com", "nopCommerce Admin Email")
    addSecretIfMissing("nop-admin-password", "Admin@12345", "nopCommerce Admin Password")
    addSecretIfMissing("nop-db-user", "sa", "nopCommerce DB User")
    addSecretIfMissing("nop-db-password", "NopCommerce@2026!", "nopCommerce DB Password")
} catch (Exception e) {
    println("Notice: Could not populate credentials: " + e.message)
}

// 2. Generate API token for admin user
User.getAll().each { u ->
    def userId = u.getId()
    if (userId == null || userId.equalsIgnoreCase("SYSTEM") || userId.equalsIgnoreCase("noreply")) {
        return
    }
    println("Processing user: " + userId + " (" + u.getFullName() + ")")
    def prop = u.getProperty(ApiTokenProperty.class)
    if (prop == null) {
        prop = new ApiTokenProperty()
        u.addProperty(prop)
    }
    def token = prop.tokenStore.generateNewToken("github-action-trigger")
    u.save()
    println("GENERATED_TOKEN=" + token.plainValue)
    new File("/var/jenkins_home/github_token.txt").text = token.plainValue
    new File("/var/jenkins_home/github_user.txt").text = userId
}
println("=== FINISHED SETUP TOKEN & CREDENTIALS SCRIPT ===")
