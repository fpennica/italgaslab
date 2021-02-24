(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('laboratorio', {
            parent: 'entity',
            url: '/laboratorio',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.laboratorio.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/laboratorio/laboratorios.html',
                    controller: 'LaboratorioController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('laboratorio');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('laboratorio-detail', {
            parent: 'entity',
            url: '/laboratorio/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.laboratorio.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/laboratorio/laboratorio-detail.html',
                    controller: 'LaboratorioDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('laboratorio');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Laboratorio', function($stateParams, Laboratorio) {
                    return Laboratorio.get({id : $stateParams.id}).$promise;
                }]
            }
        })
        .state('laboratorio.new', {
            parent: 'laboratorio',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/laboratorio/laboratorio-dialog.html',
                    controller: 'LaboratorioDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nome: null,
                                istituto: null,
                                responsabile: null,
                                indirizzo: null,
                                logo: null,
                                logoContentType: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('laboratorio', null, { reload: true });
                }, function() {
                    $state.go('laboratorio');
                });
            }]
        })
        .state('laboratorio.edit', {
            parent: 'laboratorio',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/laboratorio/laboratorio-dialog.html',
                    controller: 'LaboratorioDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Laboratorio', function(Laboratorio) {
                            return Laboratorio.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('laboratorio', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('laboratorio.delete', {
            parent: 'laboratorio',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/laboratorio/laboratorio-delete-dialog.html',
                    controller: 'LaboratorioDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Laboratorio', function(Laboratorio) {
                            return Laboratorio.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('laboratorio', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('laboratorio-teglie', {
            parent: 'entity',
            url: '/laboratorio/{id}/teglie',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.laboratorio.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/laboratorio/laboratorio-teglie.html',
                    controller: 'LaboratorioTeglieController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('laboratorio');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Laboratorio', function($stateParams, Laboratorio) {
                    return Laboratorio.get({id : $stateParams.id}).$promise;
                }]
            }
        })
        .state('laboratorio-setacci', {
            parent: 'entity',
            url: '/laboratorio/{id}/setacci',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.laboratorio.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/laboratorio/laboratorio-setacci.html',
                    controller: 'LaboratorioSetacciController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('laboratorio');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Laboratorio', function($stateParams, Laboratorio) {
                    return Laboratorio.get({id : $stateParams.id}).$promise;
                }]
            }
        });
    }

})();
