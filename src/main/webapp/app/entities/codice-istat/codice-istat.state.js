(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('codice-istat', {
            parent: 'entity',
            url: '/codice-istat?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.codiceIstat.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/codice-istat/codice-istats.html',
                    controller: 'CodiceIstatController',
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
                    $translatePartialLoader.addPart('codiceIstat');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('codice-istat-detail', {
            parent: 'entity',
            url: '/codice-istat/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.codiceIstat.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/codice-istat/codice-istat-detail.html',
                    controller: 'CodiceIstatDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('codiceIstat');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'CodiceIstat', function($stateParams, CodiceIstat) {
                    return CodiceIstat.get({id : $stateParams.id}).$promise;
                }]
            }
        })
        .state('codice-istat.new', {
            parent: 'codice-istat',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/codice-istat/codice-istat-dialog.html',
                    controller: 'CodiceIstatDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                codiceIstat: null,
                                comune: null,
                                provincia: null,
                                regione: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('codice-istat', null, { reload: true });
                }, function() {
                    $state.go('codice-istat');
                });
            }]
        })
        .state('codice-istat.edit', {
            parent: 'codice-istat',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/codice-istat/codice-istat-dialog.html',
                    controller: 'CodiceIstatDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['CodiceIstat', function(CodiceIstat) {
                            return CodiceIstat.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('codice-istat', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('codice-istat.delete', {
            parent: 'codice-istat',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/codice-istat/codice-istat-delete-dialog.html',
                    controller: 'CodiceIstatDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['CodiceIstat', function(CodiceIstat) {
                            return CodiceIstat.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('codice-istat', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
