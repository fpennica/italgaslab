(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('CassettaDetailController', CassettaDetailController);

    CassettaDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'DataUtils', 'entity', 'Cassetta', 'CodiceIstat', 'Consegna'];

    function CassettaDetailController($scope, $rootScope, $stateParams, DataUtils, entity, Cassetta, CodiceIstat, Consegna) {
        var vm = this;

        vm.cassetta = entity;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('italgaslabApp:cassettaUpdate', function(event, result) {
            vm.cassetta = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
