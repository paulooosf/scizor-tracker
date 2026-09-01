import json
import os
from datetime import datetime

def lambda_handler(event, context):
    """
    Lambda handler para processar notificações de bugs
    LocalStack: loga no console
    AWS Real: enviaria via SES
    """
    
    print(f"Lambda invocada | Records: {len(event['Records'])}")
    
    for record in event['Records']:
        try:
            sns_message = json.loads(record['body'])
            bug_data = json.loads(sns_message['Message'])
            
            print(f"Processando evento: {bug_data}")
            
            processar_notificacao(bug_data)
            
        except Exception as e:
            print(f"Erro ao processar mensagem: {str(e)}")
            print(f"Record: {json.dumps(record, indent=2)}")
    
    return {
        'statusCode': 200,
        'body': json.dumps({'processed': len(event['Records'])})
    }


def processar_notificacao(bug_data):
    """
    Processa notificação e simula envio de email
    """
    bug_id = bug_data.get('bugId', 'N/A')
    titulo = bug_data.get('titulo', 'Sem título')
    prioridade = bug_data.get('prioridade', 'MEDIA')
    projeto = bug_data.get('projetoNome', 'Projeto desconhecido')
    
    emoji_prioridade = {
        'BAIXA': '[BAIXA]',
        'MEDIA': '[MEDIA]',
        'ALTA': '[ALTA]',
        'CRITICA': '[CRITICA]'
    }.get(prioridade, '[?]')
    
    print("=" * 80)
    print(f"EMAIL SIMULADO - Bug #{bug_id}")
    print("=" * 80)
    print(f"Para: dev@scizor.com")
    print(f"Assunto: {emoji_prioridade} Bug #{bug_id}: {titulo}")
    print(f"Projeto: {projeto}")
    print(f"Prioridade: {prioridade}")
    print(f"Timestamp: {datetime.now().isoformat()}")
    print("=" * 80)
    print()
