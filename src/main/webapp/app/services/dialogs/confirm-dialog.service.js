(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .factory('ConfirmDialog', ConfirmDialog);

    ConfirmDialog.$inject = ['$uibModal'];

    function ConfirmDialog($uibModal) {
        //https://medium.com/@sebastianhenneberg/extract-mddialogs-and-uibmodal-into-dedicated-services-8a586b3e7a71#.wdimbz6rs
        return showConfirmDialog;

        function showConfirmDialog() {
            return $uibModal.open({
                template: reindexConfirmTemplate,
                controller: ['$uibModalInstance', '$scope', function($uibModalInstance) {
                    //var vm = this;

                    $scope.clear = clear;
                    $scope.confirm = confirm;

                    function clear () {
                        console.log('asdf');
                        $uibModalInstance.dismiss('cancel');
                    }

                    function confirm () {
                        $http({
            				method : 'POST',
            				url : 'api/elasticsearch/index'
            			}).then(function successCallback(response) {
                            $uibModalInstance.close(true);
            			}, function errorCallback(response) {
            				AlertService.error("Reindex error");
            			});
                    }
                }],
                //controllerAs: 'vm',
                size: 'md'
            });
        }

    }


})();
