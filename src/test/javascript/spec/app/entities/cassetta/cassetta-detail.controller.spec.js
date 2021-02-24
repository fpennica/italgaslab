'use strict';

describe('Controller Tests', function() {

    describe('Cassetta Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockCassetta, MockCodiceIstat, MockConsegna;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockCassetta = jasmine.createSpy('MockCassetta');
            MockCodiceIstat = jasmine.createSpy('MockCodiceIstat');
            MockConsegna = jasmine.createSpy('MockConsegna');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'Cassetta': MockCassetta,
                'CodiceIstat': MockCodiceIstat,
                'Consegna': MockConsegna
            };
            createController = function() {
                $injector.get('$controller')("CassettaDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'italgaslabApp:cassettaUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
