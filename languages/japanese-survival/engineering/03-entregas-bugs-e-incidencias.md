# Entregas, bugs e incidencias

Cuando surge un problema, separa siempre tres estados: investigar, corregir y
desplegar. No son equivalentes. Esta distinción evita comunicar más de lo que
sabes.

## Investigar y reproducir

> **ふぐあいを さいげん できますか。**<br>
> *fuguai o saigen dekimasu ka?*<br>
> ¿Se puede reproducir el bug?
> English: Can the bug be reproduced?

> **はい、さいげん できます。**<br>
> *hai, saigen dekimasu.*<br>
> Sí, se puede reproducir.
> English: Yes, it can be reproduced.

> **げんいんは わかりましたか。**<br>
> *gen'in wa wakarimashita ka?*<br>
> ¿Ya se conoce la causa?
> English: Do we know the cause yet?

> **まだ わかりません。ちょうさ しています。**<br>
> *mada wakarimasen. chousa shite imasu.*<br>
> Aún no lo sé. Estoy investigando.
> English: Not yet. I am investigating it.

## Corregir, probar y desplegar

> **しゅうせい しましたか。**<br>
> *shuusei shimashita ka?*<br>
> ¿Ya lo has corregido?
> English: Have you fixed it yet?

> **はい、しゅうせい しました。いま テスト しています。**<br>
> *hai, shuusei shimashita. ima tesuto shite imasu.*<br>
> Sí, lo he corregido. Ahora estoy probándolo.
> English: Yes, I have fixed it. I am testing it now.

> **リリースは いつですか。**<br>
> *ririisu wa itsu desu ka?*<br>
> ¿Cuándo es el release?
> English: When is the release?

> **あしたの よていです。**<br>
> *ashita no yotei desu.*<br>
> Está previsto para mañana.
> English: It is scheduled for tomorrow.

`しゅうせい しました` (*shuusei shimashita*) indica que aplicaste una
corrección. No afirma por sí solo que pasó todas las pruebas ni que está en
producción. Para eso, comunica también el estado de `テスト` (*tesuto*, prueba)
y `リリース` (*ririisu*, release).

## Atender una incidencia sin prometer demasiado

> **この しょうがいに たいおう できますか。**<br>
> *kono shougai ni taiou dekimasu ka?*<br>
> ¿Puedes hacerte cargo de esta incidencia?
> English: Can you handle this incident?

> **はい、たいおうします。**<br>
> *hai, taiou shimasu.*<br>
> Sí, me haré cargo / actuaré sobre ello.
> English: Yes, I will handle it.

> **もんだいは ありますか。**<br>
> *mondai wa arimasu ka?*<br>
> ¿Hay algún problema?
> English: Is there a problem?

> **いま かくにん しています。**<br>
> *ima kakunin shite imasu.*<br>
> Ahora lo estoy comprobando.
> English: I am checking it now.

`たいおうします` (*taiou shimasu*) promete que atenderás el asunto, no que ya
esté solucionado. Tras actuar, di qué hiciste y qué falta: investigar,
corregir, probar o desplegar.

## Secuencia mental mínima

| Estado | Japonés | Romaji | Mensaje honesto | English |
|---|---|---|---|---|
| investigar | 調査中（ちょうさちゅう） | chousa-chuu | todavía investigo | investigating |
| corregido | 修正済み（しゅうせいずみ） | shuusei-zumi | cambio aplicado | fixed |
| probado | テスト済み（テストずみ） | tesuto-zumi | prueba completada | tested |
| desplegado | リリース済み（リリースずみ） | ririisu-zumi | release realizado | released / deployed |

Regla práctica: usa el estado más preciso que puedas defender. Es mejor decir
“investigando” que afirmar “sin problema” antes de verificarlo.
