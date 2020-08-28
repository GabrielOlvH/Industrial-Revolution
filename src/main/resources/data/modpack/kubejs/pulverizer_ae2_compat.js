if (mod.isLoaded('appliedenergistics2')) {
  console.log('Applied Energistics 2 is present, loading Pulverizer compatibility recipes with Industrial Revolution.')
  events.listen('recipes', function (event) {
  event.recipes.indrev.pulverize({
  ingredient: {
    tag: 'c:certus_quartz_crystals'
  },
  output: {
    item: 'appliedenergistics2:certuz_quartz_dust',
    count: 1
  },
  processTime: 250
})
event.recipes.indrev.pulverize({
  ingredient: {
    tag: 'c:certus_quartz_ores'
  },
  output: {
    item: 'appliedenergistics2:certuz_quartz_dust',
    count: 5
  },
  processTime: 250
})
event.recipes.indrev.pulverize({
  ingredient: {
    item: 'appliedenergistics2:fluix_crystal'
  },
  output: {
    item: 'appliedenergistics2:fluix_dust',
    count: 1
  },
  processTime: 250
})
}