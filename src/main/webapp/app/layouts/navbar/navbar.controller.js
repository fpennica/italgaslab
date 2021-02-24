(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('NavbarController', NavbarController);

    NavbarController.$inject = ['$scope', '$state', 'Auth', 'Principal', 'ProfileService', 'LoginService', '$http', 'AlertService', 'Certificato', '$uibModal'];

    function NavbarController ($scope, $state, Auth, Principal, ProfileService, LoginService, $http, AlertService, Certificato, $uibModal) {
        var vm = this;

        vm.isNavbarCollapsed = true;
        vm.isAuthenticated = Principal.isAuthenticated;

        ProfileService.getProfileInfo().then(function(response) {
            vm.inProduction = response.inProduction;
            vm.swaggerDisabled = response.swaggerDisabled;
        });

        vm.login = login;
        vm.logout = logout;
        vm.toggleNavbar = toggleNavbar;
        vm.collapseNavbar = collapseNavbar;
        vm.$state = $state;

		getAccount();

        function getAccount() {
            Principal.identity().then(function (user) {
                if(user) {
                    vm.loginName = user.login;
                    vm.laboratorio = user.laboratorio;
                }
            });
        }

        $scope.$on('authenticationSuccess', function() {
            getAccount();
        });

        vm.reindexElasticSearch = reindexElasticSearch;
        function reindexElasticSearch() {
            $http({
				method : 'POST',
				url : 'api/elasticsearch/index'
			}).then(function successCallback(response) {
				AlertService.success("Reindex ok");
			}, function errorCallback(response) {
				AlertService.error("Reindex error");
			});

		}

        function login() {
            collapseNavbar();
            LoginService.open();
        }

        function logout() {
            collapseNavbar();
            Auth.logout();
            $state.go('home');
        }

        function toggleNavbar() {
            vm.isNavbarCollapsed = !vm.isNavbarCollapsed;
        }

        function collapseNavbar() {
            vm.isNavbarCollapsed = true;
        }
    }
})();
