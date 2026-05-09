# ✨ Lyra Aura

**A modernized experimental fork of Discord Rich Presence Android**

> Fork by [**lyraEz**](https://github.com/lyraEz)  
> Original by [**JasonBenfrin**](https://github.com/JasonBenfrin/Discord-Rich-Presence-Android)  
> Inspired by [**Kizzy (Vaibhav)**](https://www.youtube.com/channel/UCh-zsCv66gwHCIbMKLMJmaw)

---

## What is Lyra Aura?

Lyra Aura é uma fork experimental focada em estudos de:

- Discord Gateway
- Rich Presence
- WebSocket session handling
- realtime activity state
- modernização visual da aplicação
- experimentos com Compose/UI

A ideia inicial era só estudar o funcionamento interno do Rich Presence no Android.

Naturalmente isso evoluiu para uma entidade técnica questionável movida a Kotlin, lifecycle quebrado e sofrimento psicológico causado por estado assíncrono no Android. Coisa linda.

---

## Current Status

> ⚠️ **O projeto ainda NÃO está estável.**

Atualmente a aplicação passa por reestruturações internas relacionadas ao:

- Gateway lifecycle
- session persistence
- foreground service handling
- async synchronization
- bootstrap sequence
- state recovery

No estado atual ainda podem ocorrer:

- falhas durante inicialização
- crashes em runtime
- comportamento inconsistente entre builds/devices
- problemas de sincronização WebSocket
- reconexões instáveis em algumas sessões

Boa parte disso acontece porque estou:

> ***`Estudando e aplicando tudo na prática AO MESMO TEMPO dentro desse projeto.`***

Então várias partes da arquitetura ainda estão sendo refeitas conforme aprofundo certas áreas do Android e do funcionamento interno do Gateway do Discord.

---

## Development Notes

Atualmente estou com pouco tempo livre para focar totalmente no projeto.

Então várias partes ainda vão ser corrigidas/refatoradas depois conforme eu voltar a mexer com mais calma.

> **`Esse projeto funciona mais como laboratório técnico pessoal do que produto final no momento.`**

> **`Estou usando ele para aprender arquitetura Android, realtime systems e comportamento interno do Discord Gateway enquanto implemento tudo diretamente em um app real.`**

Ou seja:
sim, algumas coisas quebram violentamente no processo.

Android gosta de transformar pequenos bugs em eventos canônicos da vida do desenvolvedor. 💀

---

## Features (planned / partially implemented)

### Original features preserved
- Discord Gateway WebSocket
- Heartbeat / reconnect
- Rich Presence fields
- Status customization
- Login via WebView

### Experimental additions
- Live Presence Preview
- Quick Templates
- Presence History
- Named Presets
- Gateway Log Viewer
- Multi-theme UI
- Advanced Presence Fields
- Liquid Glass inspired interface

---

## Theme

The UI is built around a **lavender dark liquid glass** aesthetic inspired by modern iOS concepts:

- Semi-transparent glass cards
- Dynamic blur layers
- Rounded layouts
- Soft purple gradients
- AMOLED-friendly dark palette

---

## License

This project is for **educational purposes only**.

Using user tokens may violate Discord's Terms of Service.  
Use at your own risk.

---

## Credits

```txt
Lyra Aura
  Fork author   → lyraEz
  Original app  → JasonBenfrin
  Inspiration   → Kizzy by Vaibhav
```


---

> Made with 💜 by lyraEz
