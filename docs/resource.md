<h1>Reagent</h1>

A `Reagent` will be defined in the `plugins/sennetidle/regents` folder. An example regent is `stone.json`

```json
{
    "id": "stone",
    "displayName": "Stone",
    "space": 0.1,
    "value": 1.5,
    "craftingTime": 22.5,
    "craftingResources": [
        {
            "id": "otherRegentId",
            "amount": 5
        }, 
        {
            "id": "otherRegentId2",
            "amount": 3
        }
    ],
    "upgrades": [
        {
            "amountToCraft": 10,
            "craftingTimeDecrease": 0.25
        },
        {
            "amountToCraft": 50,
            "craftingTimeDecrease": 0.5
        }
    ]
}
```