(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('LaboratorioTeglieController', LaboratorioTeglieController);

    LaboratorioTeglieController.$inject = ['$scope', '$rootScope', '$stateParams', 'DataUtils', 'entity', 'Laboratorio', '$timeout'];

    function LaboratorioTeglieController($scope, $rootScope, $stateParams, DataUtils, entity, Laboratorio, $timeout) {
        var vm = this;

        vm.laboratorio = entity;

        if(!vm.laboratorio.teglie || vm.laboratorio.teglie.length === 0) {
            vm.laboratorio.teglie = "[]";
        }
        vm.teglie = JSON.parse(vm.laboratorio.teglie);

        vm.addTeglia = function() {
            var newItemNo = vm.teglie.length + 1;
            vm.teglie.push({
                'id': 'Teglia' + newItemNo,
                'peso': null
            });
        };

        vm.removeTeglia = function($index) {
            //var lastItem = vm.teglie.length - 1;
            vm.teglie.splice($index, 1);
        };

        vm.save = function() {
            vm.isSaving = true;
            vm.laboratorio.teglie = JSON.stringify(vm.teglie);
            if (vm.laboratorio.id !== null) {
                Laboratorio.update(vm.laboratorio, onSaveSuccess, onSaveError);
            } else {
                Laboratorio.save(vm.laboratorio, onSaveSuccess, onSaveError);
            }
        };

        vm.msg = null;
        function onSaveSuccess(result) {
            $scope.$emit('italgaslabApp:laboratorioUpdate', result);
            //AlertService.success("sdfhsd");
            vm.msg = "Salvataggio effettuato";
            $timeout(function(){
                vm.msg = null;
            }, 3000);
            //$uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }
    }
})();
