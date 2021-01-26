if (mod.isLoaded('techreborn')) {
    console.log('Tech Reborn is present, loading Pulverizer compatibility recipes with Industrial Revolution.')
    events.listen('recipes', function (event) {
        event.recipes.indrev.pulverize({
            ingredients: {
                tag: 'c:aluminum_ingots'
            },
            output: {
                item: 'techreborn:aluminum_dust',
                count: 1
            },
            processTime: 250
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'minecraft:andesite'
            },
            output: {
                item: 'techreborn:andesite_dust',
                count: 2
            },
            processTime: 250
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                tag: 'c:basalt'
            },
            output: {
                item: 'techreborn:basalt_dust',
                count: 2
            },
            processTime: 250
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                tag: 'c:bauxite_ores'
            },
            output: {
                item: 'techreborn:bauxite_dust',
                count: 2
            },
            processTime: 270
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                tag: 'c:brass_ingots'
            },
            output: {
                item: 'techreborn:brass_dust'
            },
            processTime: 200
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'minecraft:charcoal'
            },
            output: {
                item: 'techreborn:charcoal_dust'
            },
            processTime: 230
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'techreborn:chrome_ingot'
            },
            output: {
                item: 'techreborn:chrome_dust'
            },
            processTime: 250
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                tag: 'c:cinnabar_ores'
            },
            output: {
                item: 'techreborn:cinnabar_dust',
                count: 2
            },
            processTime: 270
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'minecraft:clay_ball'
            },
            output: {
                item: 'techreborn:clay_dust'
            },
            processTime: 180
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'minecraft:diorite'
            },
            output: {
                item: 'techreborn:diorite_dust'
            },
            processTime: 1440
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'minecraft:emerald'
            },
            output: {
                item: 'techreborn:emerald_dust'
            },
            processTime: 270
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'minecraft:ender_eye'
            },
            output: {
                item: 'techreborn:ender_eye_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'minecraft:ender_pearl'
            },
            output: {
                item: 'techreborn:ender_pearl_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'minecraft:end_stone'
            },
            output: {
                item: 'techreborn:endstone_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'minecraft:flint'
            },
            output: {
                item: 'techreborn:flint_dust'
            },
            processTime: 270
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                tag: 'c:galena_ores'
            },
            output: {
                item: 'techreborn:galena_dust',
                count: 2
            },
            processTime: 270
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: "minecraft:granite"
            },
            output: {
                item: 'techreborn:granite_dust',
                count: 2
            },
            processTime: 270
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: "techreborn:invar_ingot"
            },
            output: {
                item: 'techreborn:invar_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'minecraft:netherrack'
            },
            output: {
                item: 'techreborn:netherrack_dust'
            },
            processTime: 200
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'techreborn:nickel_ingot'
            },
            output: {
                item: 'techreborn:nickel_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'techreborn:peridot_gem'
            },
            output: {
                item: 'techreborn:peridot_dust'
            },
            processTime: 200
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                tag: 'c:peridot_ores'
            },
            output: {
                item: 'techreborn:peridot_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                tag: 'c:platinum_ingots'
            },
            output: {
                item: 'techreborn:platinum_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                tag: 'c:pyrite_ores'
            },
            output: {
                item: 'techreborn:pyrite_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'minecraft:quartz'
            },
            output: {
                item: 'techreborn:quartz_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'minecraft:quartz_block'
            },
            output: {
                item: 'techreborn:quartz_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'techreborn:red_garnet_gem'
            },
            output: {
                item: 'techreborn:red_garnet_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'techreborn:ruby_gem'
            },
            output: {
                item: 'techreborn:ruby_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                tag: 'c:ruby_ores'
            },
            output: {
                item: 'techreborn:ruby_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'techreborn:sapphire_gem'
            },
            output: {
                item: 'techreborn:sapphire_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                tag: 'c:sapphire_ores'
            },
            output: {
                item: 'techreborn:sapphire_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                tag: 'c:sodalite_ores'
            },
            output: {
                item: 'techreborn:sodalite_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                item: 'techreborn:yellow_garnet_gem'
            },
            output: {
                item: 'techreborn:yellow_garnet_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                tag: 'c:zinc_ingots'
            },
            output: {
                item: 'techreborn:zinc_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredients: {
                tag: 'c:titanium_ingots'
            },
            output: {
                item: 'techreborn:titanium_dust'
            },
            processTime: 220
        })
    })
}