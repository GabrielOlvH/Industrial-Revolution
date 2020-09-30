if (mod.isLoaded('techreborn')) {
    console.log('Tech Reborn is present, loading Infuser compatibility recipes with Industrial Revolution.')
    events.listen('recipes', function (event) {
event.recipes.indrev.infuse({
ingredients: [
{
tag: 'c:zinc_ingots'
},
{
tag: 'c:copper_ingots',
count: 3
}
],
output: {
item: 'techreborn:brass_ingot',
count: 4
},
processTime: 200
})

event.recipes.indrev.infuse({
ingredients: [
{
tag: 'c:zinc_dusts'
},
{
tag: 'c:copper_dusts',
count: 3
}
],
output: {
item: 'techreborn:brass_ingot',
count: 4
},
processTime: 200
})

event.recipes.indrev.infuse({
ingredients: [
{
tag: 'c:zinc_dusts'
},
{
tag: 'c:copper_ingots',
count: 3
}
],
output: {
item: 'techreborn:brass_ingot',
count: 4
},
processTime: 200
})

event.recipes.indrev.infuse({
ingredients: [
{
tag: 'c:zinc_ingots'
},
{
tag: 'c:copper_dusts',
count: 3
}
],
output: {
item: 'techreborn:brass_ingot',
count: 4
},
processTime: 200
})

event.recipes.indrev.infuse({
ingredients: [
{
tag: 'c:tin_ingots'
},
{
tag: 'c:copper_ingots',
count: 3
}
],
output: {
item: 'techreborn:bronze_ingot',
count: 4
},
processTime: 200
})

event.recipes.indrev.infuse({
ingredients: [
{
tag: 'c:tin_dusts'
},
{
tag: 'c:copper_ingots',
count: 3
}
],
output: {
item: 'techreborn:bronze_ingot',
count: 4
},
processTime: 200
})

event.recipes.indrev.infuse({
ingredients: [
{
tag: 'c:tin_dusts'
},
{
tag: 'c:copper_dusts',
count: 3
}
],
output: {
item: 'techreborn:bronze_ingot',
count: 4
},
processTime: 200
})

event.recipes.indrev.infuse({
ingredients: [
{
tag: 'c:tin_ingots'
},
{
tag: 'c:copper_dusts',
count: 3
}
],
output: {
item: 'techreborn:bronze_ingot',
count: 4
},
processTime: 200
})

event.recipes.indrev.infuse({
ingredients: [
{
item: 'minecraft:gold_ingot'
},
{
tag: 'c:silver_ingots'
}
],
output: {
item: 'techreborn:electrum_ingot',
count: 2
},
processTime: 200
})

event.recipes.indrev.infuse({
ingredients: [
{
tag: 'c:gold_dusts'
},
{
tag: 'c:silver_ingots'
}
],
output: {
item: 'techreborn:electrum_ingot',
count: 2
},
processTime: 200
})

event.recipes.indrev.infuse({
ingredients: [
{
item: 'minecraft:gold_ingot'
},
{
tag: 'c:silver_dusts'
}
],
output: {
item: 'techreborn:electrum_ingot',
count: 2
},
processTime: 200
})

event.recipes.indrev.infuse({
ingredients: [
{
tag: 'c:gold_dusts'
},
{
tag: 'c:silver_dusts'
}
],
output: {
item: 'techreborn:electrum_ingot',
count: 2
},
processTime: 200
})

event.recipes.indrev.infuse({
ingredients: [
{
item: 'minecraft:iron_ingot',
count: 2
},
{
item: 'techreborn:nickel_ingot'
}
],
output: {
item: 'techreborn:invar_ingot',
count: 3
},
processTime: 200
})

event.recipes.indrev.infuse({
ingredients: [
{
tag: 'c:iron_dusts',
count: 2
},
{
item: 'techreborn:nickel_dust'
}
],
output: {
item: 'techreborn:invar_ingot',
count: 3
},
processTime: 200
})

    })
}
