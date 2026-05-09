# ✨ Lyra Aura

**Uma fork experimental modernizada do Discord Rich Presence Android**

> Fork por [**lyraEz**](https://github.com/lyraEz)  
> Projeto original por [**JasonBenfrin**](https://github.com/JasonBenfrin/Discord-Rich-Presence-Android)  
> Inspirado por [**Kizzy (Vaibhav)**](https://www.youtube.com/channel/UCh-zsCv66gwHCIbMKLMJmaw)

---

## O que é o Lyra Aura?

Lyra Aura é uma fork experimental focada em estudos de:

- Discord Gateway
- Rich Presence
- gerenciamento de sessão WebSocket
- sincronização de estado em tempo real
- arquitetura Android moderna
- experimentos visuais com Compose/UI

A ideia inicial era apenas estudar o funcionamento interno do Rich Presence no Android.

Naturalmente isso evoluiu para uma entidade técnica questionável movida a Kotlin, lifecycle quebrado, sincronização assíncrona e sofrimento psicológico causado pelo ecossistema Android moderno. Coisa linda.

---

## Estado Atual

> ⚠️ **O projeto ainda NÃO está estável.**

Atualmente a aplicação passa por reestruturações internas relacionadas a:

- lifecycle do Gateway
- persistência de sessão
- gerenciamento de foreground service
- sincronização assíncrona
- sequência de bootstrap da aplicação
- recuperação de estado em runtime

No estado atual ainda podem ocorrer:

- falhas durante inicialização
- crashes em runtime
- comportamento inconsistente entre builds/dispositivos
- problemas de sincronização WebSocket
- reconexões instáveis em algumas sessões

Boa parte disso acontece porque estou:

> ***`Estudando e aplicando tudo na prática AO MESMO TEMPO dentro desse projeto.`***

Então várias partes da arquitetura ainda estão sendo refeitas conforme aprofundo certas áreas do Android e do funcionamento interno do Gateway do Discord.

---

## Notas de Desenvolvimento

Atualmente estou com pouco tempo livre para focar totalmente no projeto.

Então várias partes ainda vão ser corrigidas/refatoradas depois conforme eu conseguir voltar a mexer nele com mais calma.

> **`Esse projeto funciona mais como laboratório técnico pessoal do que produto final neste momento.`**

> **`Estou utilizando ele para aprender arquitetura Android, sistemas realtime e comportamento interno do Discord Gateway enquanto implemento tudo diretamente em uma aplicação real.`**

Ou seja:

sim, algumas coisas quebram violentamente no processo.

Android gosta de transformar pequenos bugs em eventos canônicos na vida do desenvolvedor. 💀

---

## Funcionalidades (planejadas / parcialmente implementadas)

### Funcionalidades originais preservadas
- WebSocket do Discord Gateway
- heartbeat / reconnect
- campos completos de Rich Presence
- personalização de status
- login via WebView

### Adições experimentais
- pré-visualização da Presence em tempo real
- templates rápidos
- histórico de Presence
- presets personalizados
- visualizador de logs do Gateway
- interface multi-tema
- campos avançados de Presence
- interface inspirada em Liquid Glass

---

## Interface

A interface foi construída em torno de uma estética **lavender dark liquid glass**, inspirada em conceitos modernos de UI:

- cartões semi-transparentes
- camadas de blur dinâmico
- layouts arredondados
- gradientes suaves em tons de roxo
- visual otimizado para AMOLED

---

## Licença

Este projeto existe apenas para fins educacionais.

O uso de user tokens pode violar os Termos de Serviço do Discord.  
Use por sua conta e risco.

---

## Créditos

```txt
Lyra Aura
  Fork author   → lyraEz
  Projeto base  → JasonBenfrin
  Inspiração    → Kizzy by Vaibhav
```

---

> Made with 💜 by lyraEz
