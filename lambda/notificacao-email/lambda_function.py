import json
import os
from datetime import datetime

def lambda_handler(event, context):
    """
    Lambda handler para processar notificações
    LocalStack: loga no console
    AWS Real: enviaria via SES
    """
    
    print(f"Lambda invocada | Records: {len(event['Records'])}")
    
    for record in event['Records']:
        try:
            sns_message = json.loads(record['body'])
            event_data = json.loads(sns_message['Message'])
            
            print(f"Processando evento: {event_data}")
            
            if 'bugId' in event_data:
                processar_notificacao_bug(event_data)
            elif 'token' in event_data and 'email' in event_data:
                processar_redefinicao_senha(event_data)
            else:
                print(f"Tipo de evento desconhecido: {event_data}")
            
        except Exception as e:
            print(f"Erro ao processar mensagem: {str(e)}")
            print(f"Record: {json.dumps(record, indent=2)}")
    
    return {
        'statusCode': 200,
        'body': json.dumps({'processed': len(event['Records'])})
    }


def processar_notificacao_bug(bug_data):
    """
    Processa notificação de bug e simula envio de email
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


def processar_redefinicao_senha(senha_data):
    """
    Processa solicitação de redefinição de senha e simula envio de email
    """
    email = senha_data.get('email', 'N/A')
    token = senha_data.get('token', 'N/A')
    data_solicitacao = senha_data.get('dataSolicitacao', 'N/A')
    
    link_redefinicao = f"http://localhost:8080/redefinir-senha?token={token}"
    
    print("=" * 80)
    print(f"EMAIL SIMULADO - Redefinicao de Senha")
    print("=" * 80)
    print(f"Para: {email}")
    print(f"Assunto: Solicitacao de Redefinicao de Senha - Scizor Tracker")
    print(f"Data Solicitacao: {data_solicitacao}")
    print("")
    print("Mensagem:")
    print("  Voce solicitou a redefinicao de senha.")
    print("  Clique no link abaixo para redefinir sua senha:")
    print(f"  {link_redefinicao}")
    print("")
    print("  Se voce nao solicitou esta redefinicao, ignore este email.")
    print("  O link expira em 1 hora.")
    print(f"Timestamp: {datetime.now().isoformat()}")
    print("=" * 80)
    print()
