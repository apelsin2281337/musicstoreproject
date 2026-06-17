import { test, expect } from "@playwright/test";
import { RegistrationPage } from "./pages/registration.page";
import { LoginPage } from "./pages/login.page";
import { HomePage } from "./pages/homepage.page";
import { CartPage } from "./pages/cartpage.page";
import { LibraryPage } from "./pages/library.page";

test("Track purchase flow test", async ({page}) =>{
    const registrationPage = new RegistrationPage(page);
    const loginPage = new LoginPage(page)
    const homePage = new HomePage(page)
    const cartPage = new CartPage(page)
    const libraryPage = new LibraryPage(page)

    const username = `testuser_${Date.now()}`
    const password = "testpassword11"

    await registrationPage.open()
    await registrationPage.register(username, password)

    await expect(registrationPage.successToast).toContainText("Регистрация успешна!")

    await registrationPage.loginButton.click()

    await loginPage.login(username, password)


    await expect(homePage.recommendedTracksText).toContainText("Рекомендованные треки")
    await homePage.trackToCart(1)
    await homePage.cartButton.click()

    await expect(cartPage.cartItems).not.toBeEmpty()
    await cartPage.checkoutButton.click()
    await cartPage.libraryButton.click()

    const downloadPromise = page.waitForEvent('download')

    await libraryPage.downloadTrackByIndex(0)

    const download = await downloadPromise

    expect(download.suggestedFilename()).toBeTruthy()
})

