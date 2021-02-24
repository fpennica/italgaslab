(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('consegna', {
            parent: 'entity',
            url: '/consegna?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.consegna.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/consegna/consegnas.html',
                    controller: 'ConsegnaController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('consegna');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('consegna-detail', {
            parent: 'entity',
            url: '/consegna/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.consegna.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/consegna/consegna-detail.html',
                    controller: 'ConsegnaDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('consegna');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Consegna', function($stateParams, Consegna) {
                    return Consegna.get({id : $stateParams.id}).$promise;
                }]
            }
        })
        .state('consegna.new', {
            parent: 'consegna',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/consegna/consegna-dialog.html',
                    controller: 'ConsegnaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                dataConsegna: null,
                                trasportatore: null,
                                numCassette: null,
                                numProtocolloAccettazione: null,
                                protocolloAccettazione: null,
                                protocolloAccettazioneContentType: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('consegna', null, { reload: true });
                }, function() {
                    $state.go('consegna');
                });
            }]
        })
        .state('consegna.edit', {
            parent: 'consegna',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/consegna/consegna-dialog.html',
                    controller: 'ConsegnaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Consegna', function(Consegna) {
                            return Consegna.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('consegna', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('consegna.delete', {
            parent: 'consegna',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/consegna/consegna-delete-dialog.html',
                    controller: 'ConsegnaDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Consegna', function(Consegna) {
                            return Consegna.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('consegna', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
