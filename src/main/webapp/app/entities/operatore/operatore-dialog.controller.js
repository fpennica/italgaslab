(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('OperatoreDialogController', OperatoreDialogController);

    OperatoreDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', '$q', 'entity', 'Operatore', 'User', 'Laboratorio'];

    function OperatoreDialogController ($timeout, $scope, $stateParams, $uibModalInstance, $q, entity, Operatore, User, Laboratorio) {
        var vm = this;

        vm.operatore = entity;
        vm.clear = clear;
        vm.save = save;
        vm.users = User.query();
        vm.laboratorios = Laboratorio.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.operatore.id !== null) {
                Operatore.update(vm.operatore, onSaveSuccess, onSaveError);
            } else {
                Operatore.save(vm.operatore, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('italgaslabApp:operatoreUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
