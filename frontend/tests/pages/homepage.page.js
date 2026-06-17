export class HomePage {
    constructor(page){
        this.page = page;
        this.registerButton = page.getByTestId("header-register")
        this.catalogueButton = page.getByTestId("nav-browse")
        this.recommendedTracksList = page.getByTestId("recommended-tracks-grid").getByTestId('track-card')
        this.cartButton = page.getByTestId("header-cart")
        this.recommendedTracksText = page.getByTestId("section-recommended-title")
    }   

    async trackToCart(index) {
        const track = this.recommendedTracksList.nth(index)
        await track.getByTestId('track-add-cart-btn').click()
    }
}