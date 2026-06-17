export class CartPage {
    constructor(page) {
        this.page = page
        this.checkoutButton = page.getByTestId("cart-checkout-btn")
        this.cartItems = page.getByTestId("cart-items")
        this.libraryButton = page.getByTestId("nav-library")
    }
}