// Dispense linear data with noise for Spark to guess the gradient of
const express = require('express')
const randgen = require('randgen')
const app = express()


function generateData(time) {
    return 10.0*time + randgen.rnorm(0, 1)
}

app.get('/', (req, res) => {
            const time = (new Date()).getTime()
            res.set({ 'content-type': 'application/json;charset=utf-8' })
            res.json({"index": generateData(time), "timestamp": time})
        }
    )

app.listen(3000, () => console.log('Data dispenser app listening on port 3000!'))

//{
//  "sources": [
//    {
//      "sourceName": "iot_data",
//      "sourceURL": "localhost:3000",
//      "webSocket": false,
//      "jsonSelectionThing": "tbc",
//      "pollingFrequencySeconds": "1"
//    }
//  ],
//  "mlAlgorithm": "linear_regression"
//}