'use strict';

describe('Controller Tests', function() {

    describe('Consegna Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockConsegna, MockLaboratorio;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockConsegna = jasmine.createSpy('MockConsegna');
            MockLaboratorio = jasmine.createSpy('MockLaboratorio');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'Consegna': MockConsegna,
                'Laboratorio': MockLaboratorio
            };
            createController = function() {
                $injector.get('$controller')("ConsegnaDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'italgaslabApp:consegnaUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
