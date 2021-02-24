(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('PesataDialogController', PesataDialogController);

    PesataDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Pesata', 'Campione'];

    function PesataDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Pesata, Campione) {
        var vm = this;

        vm.pesata = entity;
        vm.clear = clear;
        vm.save = save;
        vm.campiones = Campione.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.pesata.id !== null) {
                Pesata.update(vm.pesata, onSaveSuccess, onSaveError);
            } else {
                Pesata.save(vm.pesata, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('italgaslabApp:pesataUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
