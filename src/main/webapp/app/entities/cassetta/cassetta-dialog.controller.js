(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('CassettaDialogController', CassettaDialogController);

    CassettaDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'Cassetta', 'CodiceIstat', 'Consegna', 'CodiceIstatSearch'];

    function CassettaDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, Cassetta, CodiceIstat, Consegna, CodiceIstatSearch) {
        var vm = this;

        vm.loadCodiciIstat = loadCodiciIstat;
        function loadCodiciIstat(query) {
        	return CodiceIstatSearch.query({
        		query: query
        	}).$promise;
        }

        vm.cassetta = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.save = save;
        vm.codiceistats = CodiceIstat.query();
        vm.consegnas = Consegna.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.cassetta.id !== null) {
                Cassetta.update(vm.cassetta, onSaveSuccess, onSaveError);
            } else {
                Cassetta.save(vm.cassetta, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('italgaslabApp:cassettaUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dataScavo = false;

        vm.setFotoContenitore = function ($file, cassetta) {
            if ($file && $file.$error === 'pattern') {
                return;
            }
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        cassetta.fotoContenitore = base64Data;
                        cassetta.fotoContenitoreContentType = $file.type;
                    });
                });
            }
        };

        vm.setFotoContenuto = function ($file, cassetta) {
            if ($file && $file.$error === 'pattern') {
                return;
            }
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        cassetta.fotoContenuto = base64Data;
                        cassetta.fotoContenutoContentType = $file.type;
                    });
                });
            }
        };

        vm.setCertificatoPdf = function ($file, cassetta) {
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        cassetta.certificatoPdf = base64Data;
                        cassetta.certificatoPdfContentType = $file.type;
                    });
                });
            }
        };

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
