export class LoginPage {
    constructor(page) {
        this.page = page
        this.usernameField = page.getByTestId("login-username-input")
        this.passwordField = page.getByTestId("login-password-input")
        this.LoginPageButton = page.getByTestId("login-submit-btn")
    }

    async login(username, password){
        await this.usernameField.fill(username)
        await this.passwordField.fill(password)
        await this.LoginPageButton.click()
    }
}