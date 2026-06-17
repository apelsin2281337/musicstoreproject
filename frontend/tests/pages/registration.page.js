export class RegistrationPage {
    constructor(page){
        this.page = page
        this.usernameField = page.getByTestId("register-username-input")
        this.passwordField = page.getByTestId("register-password-input")
        this.registerButton = page.getByTestId("register-submit-btn")
        this.loginButton = page.getByTestId("header-login")
        this.successToast = page.getByTestId("register-success")
    }

    async open(){
        await this.page.goto("http://localhost:5173/register")
    }


    async register(username, password){
        await this.usernameField.fill(username)
        await this.passwordField.fill(password)
        await this.registerButton.click()
    }
}