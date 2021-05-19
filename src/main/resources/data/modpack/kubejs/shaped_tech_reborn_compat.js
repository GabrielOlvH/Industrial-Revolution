if (Platform.isLoaded('techreborn')) {
    console.log('Tech Reborn is present, loading Shaped Recipes compatibility recipes with Industrial Revolution.')
    events.listen('recipes', function (event) {
        event.recipes.minecraft.crafting_shaped({
            pattern: [
                " C ",
                "C C",
                " C "
            ],
            key: {
                C: {
                    item: "indrev:coal_dust"
                }
            },
            result: {
                item: "techreborn:carbon_fiber"
            }
        })
    })
}