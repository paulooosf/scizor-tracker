#!/bin/bash

echo "Inicializando recursos AWS no LocalStack..."

awslocal sns create-topic --name bug-notificacoes

awslocal sqs create-queue --queue-name bug-notificacoes-queue

TOPIC_ARN=$(awslocal sns list-topics --query "Topics[?contains(TopicArn, 'bug-notificacoes')].TopicArn" --output text)
QUEUE_URL=$(awslocal sqs list-queues --query "QueueUrls[?contains(@, 'bug-notificacoes-queue')]" --output text)
QUEUE_ARN=$(awslocal sqs get-queue-attributes --queue-url $QUEUE_URL --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)

awslocal sqs set-queue-attributes \
  --queue-url $QUEUE_URL \
  --attributes '{
    "Policy": "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"Service\":\"sns.amazonaws.com\"},\"Action\":\"sqs:SendMessage\",\"Resource\":\"'$QUEUE_ARN'\"}]}"
  }'

awslocal sns subscribe \
  --topic-arn $TOPIC_ARN \
  --protocol sqs \
  --notification-endpoint $QUEUE_ARN

cd /tmp
mkdir -p lambda-package
cp /docker-entrypoint-initaws.d/lambda_function.py lambda-package/
cd lambda-package
zip -r ../lambda-package.zip .
cd ..

awslocal lambda create-function \
  --function-name notificacao-email-handler \
  --runtime python3.11 \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --handler lambda_function.lambda_handler \
  --zip-file fileb:///tmp/lambda-package.zip \
  --timeout 30

awslocal lambda create-event-source-mapping \
  --function-name notificacao-email-handler \
  --event-source-arn $QUEUE_ARN \
  --batch-size 10

echo "Recursos criados com sucesso!"
echo "SNS Topic ARN: $TOPIC_ARN"
echo "SQS Queue URL: $QUEUE_URL"
echo "Lambda Function: notificacao-email-handler"
