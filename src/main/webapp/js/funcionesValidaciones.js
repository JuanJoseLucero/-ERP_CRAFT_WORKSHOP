/**
 * Evita que el formulario se envíe al presionar Enter en inputs de texto.
 * @param {Event} event - El evento del teclado
 */
function stopEnterKey(event) {
    // 1. Obtener el código de la tecla
    var key = event.which || event.keyCode;

    // 2. Si la tecla es Enter (13)
    if (key === 13) {
        var element = event.target;

        // 3. Verificar que no sea un botón ni un textarea
        // (En los botones queremos que Enter siga funcionando para activarlos)
        var isButton = element.type === 'submit' || element.type === 'button';
        var isTextarea = element.tagName.toLowerCase() === 'textarea';

        if (!isButton && !isTextarea) {
            // 4. Detener el envío del formulario
            if (event.preventDefault) {
                event.preventDefault();
            } else {
                event.returnValue = false; // Soporte para navegadores muy antiguos
            }
            return false;
        }
    }
    return true;
}