if (mod.isLoaded('techreborn')) {
    console.log('Tech Reborn is present, loading Pulverizer compatibility recipes with Industrial Revolution.')
    events.listen('recipes', function (event) {
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:aluminum_ingots'
            },
            output: {
                item: 'techreborn:aluminum_dust',
                count: 1
            },
            processTime: 250
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'minecraft:andesite'
            },
            output: {
                item: 'techreborn:andesite_dust',
                count: 2
            },
            processTime: 250
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:basalt'
            },
            output: {
                item: 'techreborn:basalt_dust',
                count: 2
            },
            processTime: 250
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:bauxite_ores'
            },
            output: {
                item: 'techreborn:bauxite_dust',
                count: 2
            },
            processTime: 270
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:brass_ingots'
            },
            output: {
                item: 'techreborn:brass_dust'
            },
            processTime: 200
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:bronze_ingots'
            },
            output: {
                item: 'techreborn:bronze_dust'
            },
            processTime: 200
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'minecraft:charcoal'
            },
            output: {
                item: 'techreborn:charcoal_dust'
            },
            processTime: 230
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'techreborn:chrome_ingot'
            },
            output: {
                item: 'techreborn:chrome_dust'
            },
            processTime: 250
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:cinnabar_ores'
            },
            output: {
                item: 'techreborn:cinnabar_dust',
                count: 2
            },
            processTime: 270
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'minecraft:clay_ball'
            },
            output: {
                item: 'techreborn:clay_dust'
            },
            processTime: 180
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'minecraft:diorite'
            },
            output: {
                item: 'techreborn:diorite_dust'
            },
            processTime: 1440
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:electrum_ingots'
            },
            output: {
                item: 'techreborn:electrum_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'minecraft:emerald'
            },
            output: {
                item: 'techreborn:emerald_dust'
            },
            processTime: 270
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'minecraft:ender_eye'
            },
            output: {
                item: 'techreborn:ender_eye_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'minecraft:ender_pearl'
            },
            output: {
                item: 'techreborn:ender_pearl_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'minecraft:end_stone'
            },
            output: {
                item: 'techreborn:endstone_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'minecraft:flint'
            },
            output: {
                item: 'techreborn:flint_dust'
            },
            processTime: 270
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:galena_ores'
            },
            output: {
                item: 'techreborn:galena_dust',
                count: 2
            },
            processTime: 270
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: "minecraft:granite"
            },
            output: {
                item: 'techreborn:granite_dust',
                count: 2
            },
            processTime: 270
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: "techreborn:invar_ingot"
            },
            output: {
                item: 'techreborn:invar_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:lead_ingots'
            },
            output: {
                item: 'techreborn:lead_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:lead_ores'
            },
            output: {
                item: 'techreborn:lead_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'minecraft:netherrack'
            },
            output: {
                item: 'techreborn:netherrack_dust'
            },
            processTime: 200
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'techreborn:nickel_ingot'
            },
            output: {
                item: 'techreborn:nickel_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'techreborn:peridot_gem'
            },
            output: {
                item: 'techreborn:peridot_dust'
            },
            processTime: 200
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:peridot_ores'
            },
            output: {
                item: 'techreborn:peridot_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:platinum_ingots'
            },
            output: {
                item: 'techreborn:platinum_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:pyrite_ores'
            },
            output: {
                item: 'techreborn:pyrite_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'minecraft:quartz'
            },
            output: {
                item: 'techreborn:quartz_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'minecraft:quartz_block'
            },
            output: {
                item: 'techreborn:quartz_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'techreborn:red_garnet_gem'
            },
            output: {
                item: 'techreborn:red_garnet_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'techreborn:ruby_gem'
            },
            output: {
                item: 'techreborn:ruby_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:ruby_ores'
            },
            output: {
                item: 'techreborn:ruby_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'techreborn:sapphire_gem'
            },
            output: {
                item: 'techreborn:sapphire_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:sapphire_ores'
            },
            output: {
                item: 'techreborn:sapphire_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:silver_ingots'
            },
            output: {
                item: 'techreborn:silver_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:silver_ores'
            },
            output: {
                item: 'techreborn:silver_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:sodalite_ores'
            },
            output: {
                item: 'techreborn:sodalite_dust',
                count: 2
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:tungsten_ingots'
            },
            output: {
                item: 'techreborn:tungsten_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                item: 'techreborn:yellow_garnet_gem'
            },
            output: {
                item: 'techreborn:yellow_garnet_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:zinc_ingots'
            },
            output: {
                item: 'techreborn:zinc_dust'
            },
            processTime: 220
        })
        event.recipes.indrev.pulverize({
            ingredient: {
                tag: 'c:titanium_ingots'
            },
            output: {
                item: 'techreborn:titanium_dust'
            },
            processTime: 220
        })
    })
}