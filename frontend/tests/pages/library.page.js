export class LibraryPage {
    constructor(page) {
        this.page = page
        this.trackItems = page.getByTestId('library-track-list').getByTestId('library-track-item')
    }

    async downloadTrackByIndex(index){
        const track = this.trackItems.nth(index)
        await track.getByTestId("library-download-btn").click()
    }
}