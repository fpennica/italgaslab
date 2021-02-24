(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('operatore', {
            parent: 'entity',
            url: '/operatore',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.operatore.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/operatore/operatores.html',
                    controller: 'OperatoreController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('operatore');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('operatore-detail', {
            parent: 'entity',
            url: '/operatore/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.operatore.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/operatore/operatore-detail.html',
                    controller: 'OperatoreDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('operatore');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Operatore', function($stateParams, Operatore) {
                    return Operatore.get({id : $stateParams.id}).$promise;
                }]
            }
        })
        .state('operatore.new', {
            parent: 'operatore',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operatore/operatore-dialog.html',
                    controller: 'OperatoreDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('operatore', null, { reload: true });
                }, function() {
                    $state.go('operatore');
                });
            }]
        })
        .state('operatore.edit', {
            parent: 'operatore',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operatore/operatore-dialog.html',
                    controller: 'OperatoreDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Operatore', function(Operatore) {
                            return Operatore.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('operatore', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('operatore.delete', {
            parent: 'operatore',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operatore/operatore-delete-dialog.html',
                    controller: 'OperatoreDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Operatore', function(Operatore) {
                            return Operatore.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('operatore', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
