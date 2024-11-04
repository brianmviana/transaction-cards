#!/bin/bash

aws dynamodb create-table \
  --table-name Transactions \
  --attribute-definitions \
    AttributeName=transactionId,AttributeType=S \
    AttributeName=accountId,AttributeType=S \
  --key-schema \
    AttributeName=transactionId,KeyType=HASH \
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
  --global-secondary-indexes '[
   {
     "IndexName": "AccountIndex",
     "KeySchema": [
       {
         "AttributeName": "accountId",
         "KeyType": "HASH"
       }
     ],
     "Projection": {
       "ProjectionType": "ALL"
     },
     "ProvisionedThroughput": {
       "ReadCapacityUnits": 5,
       "WriteCapacityUnits": 5
     }
   }
 ]' \
  --endpoint-url http://localhost:4566 \
  --region sa-east-1


# ---------

aws dynamodb create-table \
    --table-name Merchants \
    --attribute-definitions \
        AttributeName=merchantName,AttributeType=S \
    --key-schema \
        AttributeName=merchantName,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --endpoint-url http://localhost:4566 \
    --region sa-east-1

aws dynamodb put-item \
  --table-name Merchants \
  --item '{
    "merchantName": {"S": "UBER TRIP                   SAO PAULO BR"},
    "mcc": {"N": "1234"}
  }' \
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
  --endpoint-url http://localhost:4566 \
  --region sa-east-1

aws dynamodb put-item \
  --table-name Merchants \
  --item '{
    "merchantName": {"S": "UBER EATS                   SAO PAULO BR"},
    "mcc": {"N": "5811"}
  }' \
  --endpoint-url http://localhost:4566 \
  --region sa-east-1

aws dynamodb put-item \
  --table-name Merchants \
  --item '{
    "merchantName": {"S": "PAG*JoseDaSilva          RIO DE JANEI BR"},
    "mcc": {"N": "147"}
  }' \
  --endpoint-url http://localhost:4566 \
  --region sa-east-1

aws dynamodb put-item \
  --table-name Merchants \
  --item '{
    "merchantName": {"S": "PICPAY*BILHETEUNICO           GOIANIA BR"},
    "mcc": {"N": "2505"}
  }' \
  --endpoint-url http://localhost:4566 \
  --region sa-east-1

aws dynamodb put-item \
  --table-name Merchants \
  --item '{
    "merchantName": {"S": "PADARIA DO ZE               SAO PAULO BR"},
    "mcc": {"N": "5812"}
  }' \
  --endpoint-url http://localhost:4566 \
  --region sa-east-1


# ---------

aws dynamodb create-table \
  --table-name Cards \
  --attribute-definitions \
    AttributeName=accountId,AttributeType=S \
  --key-schema \
    AttributeName=accountId,KeyType=HASH \
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
  --endpoint-url http://localhost:4566 \
  --region sa-east-1

aws dynamodb put-item \
  --table-name Cards \
  --item '{
    "accountId": {"S": "123"},
    "foodBalance": {"N": "250.00"},
    "mealBalance": {"N": "150.00"},
    "cashBalance": {"N": "2000.00"}
  }' \
  --endpoint-url http://localhost:4566 \
  --region sa-east-1

aws dynamodb put-item \
  --table-name Cards \
  --item '{
    "accountId": {"S": "456"},
    "foodBalance": {"N": "50.00"},
    "mealBalance": {"N": "300.00"},
    "cashBalance": {"N": "100.00"}
  }' \
  --endpoint-url http://localhost:4566 \
  --region sa-east-1