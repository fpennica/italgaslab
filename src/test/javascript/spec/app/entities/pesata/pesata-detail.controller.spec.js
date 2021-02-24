'use strict';

describe('Controller Tests', function() {

    describe('Pesata Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPesata, MockCampione;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPesata = jasmine.createSpy('MockPesata');
            MockCampione = jasmine.createSpy('MockCampione');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'Pesata': MockPesata,
                'Campione': MockCampione
            };
            createController = function() {
                $injector.get('$controller')("PesataDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'italgaslabApp:pesataUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
