(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('CodiceIstatDialogController', CodiceIstatDialogController);

    CodiceIstatDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'CodiceIstat'];

    function CodiceIstatDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, CodiceIstat) {
        var vm = this;

        vm.codiceIstat = entity;
        vm.clear = clear;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.codiceIstat.id !== null) {
                CodiceIstat.update(vm.codiceIstat, onSaveSuccess, onSaveError);
            } else {
                CodiceIstat.save(vm.codiceIstat, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('italgaslabApp:codiceIstatUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
