(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .factory('LaboratorioSearch', LaboratorioSearch);

    LaboratorioSearch.$inject = ['$resource'];

    function LaboratorioSearch($resource) {
        var resourceUrl =  'api/_search/laboratorios/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
