(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('LaboratorioDetailController', LaboratorioDetailController);

    LaboratorioDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'DataUtils', 'entity', 'Laboratorio'];

    function LaboratorioDetailController($scope, $rootScope, $stateParams, DataUtils, entity, Laboratorio) {
        var vm = this;

        vm.laboratorio = entity;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('italgaslabApp:laboratorioUpdate', function(event, result) {
            vm.laboratorio = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
