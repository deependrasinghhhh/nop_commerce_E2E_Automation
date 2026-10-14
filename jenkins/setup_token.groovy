import jenkins.model.Jenkins
import hudson.model.User
import jenkins.security.ApiTokenProperty

println("=== RUNNING SETUP TOKEN SCRIPT ===")
User.getAll().each { u ->
    println("Found user: " + u.getId() + " (" + u.getFullName() + ")")
    def prop = u.getProperty(ApiTokenProperty.class)
    if (prop == null) {
        prop = new ApiTokenProperty()
        u.addProperty(prop)
    }
    def token = prop.tokenStore.generateNewToken("github-action-trigger")
    u.save()
    println("GENERATED_TOKEN=" + token.plainValue)
    new File("/var/jenkins_home/github_token.txt").text = token.plainValue
    new File("/var/jenkins_home/github_user.txt").text = u.getId()
}
println("=== FINISHED SETUP TOKEN SCRIPT ===")
