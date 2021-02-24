'use strict';

describe('Controller Tests', function() {

    describe('Laboratorio Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockLaboratorio;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockLaboratorio = jasmine.createSpy('MockLaboratorio');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'Laboratorio': MockLaboratorio
            };
            createController = function() {
                $injector.get('$controller')("LaboratorioDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'italgaslabApp:laboratorioUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
