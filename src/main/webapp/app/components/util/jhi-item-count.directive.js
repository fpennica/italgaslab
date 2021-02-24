(function() {
    'use strict';

    var jhiItemCount = {
        template: '<div class="info">' +
                    'Visualizzati {{(($ctrl.page - 1) * $ctrl.itemsPerPage) == 0 ? 1 : (($ctrl.page - 1) * $ctrl.itemsPerPage + 1)}} - ' +
                    '{{($ctrl.page * $ctrl.itemsPerPage) < $ctrl.queryCount ? ($ctrl.page * $ctrl.itemsPerPage) : $ctrl.queryCount}} ' +
                    'di {{$ctrl.queryCount}} oggetti.' +
                '</div>',
        bindings: {
            page: '<',
            queryCount: '<total',
            itemsPerPage: '<'
        }
    };

    angular
        .module('italgaslabApp')
        .component('jhiItemCount', jhiItemCount);
})();
