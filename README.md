# Experimental Menu

Mod client-side para **Minecraft Java 1.21.4**, Fabric Loader e Fabric API 0.119.5+1.21.4. Requer Java 21.

## Recursos
- Menu escuro aberto ao pressionar SHIFT; ESC fecha.
- ESP Box visual em entidades vivas.
- AIM BOT ARC reserva a opção para indicadores de treino; esta versão inicial não move a mira automaticamente.
- PULO BLZ aplica um pequeno impulso horizontal ao iniciar um salto enquanto estiver ativado. É experimental e não é um efeito de poção real; em multiplayer o servidor pode corrigir o movimento.

## Compilar
Use Java 21 e Gradle 8.12 ou compatível com Fabric Loom 1.9:

```sh
gradle build
```

O JAR estará em `build/libs/experimental-menu-1.0.0.jar`.

## Instalar
1. Instale Fabric Loader para Minecraft 1.21.4.
2. Instale Fabric API 0.119.5+1.21.4.
3. Copie o JAR para `.minecraft/mods`.
4. Inicie o perfil Fabric 1.21.4.

Teste primeiro em single-player e faça backup do mundo. O projeto ainda precisa passar pela compilação automática do GitHub antes de ser considerado validado.
