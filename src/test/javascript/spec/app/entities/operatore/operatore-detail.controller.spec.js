'use strict';

describe('Controller Tests', function() {

    describe('Operatore Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockOperatore, MockUser, MockLaboratorio;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockOperatore = jasmine.createSpy('MockOperatore');
            MockUser = jasmine.createSpy('MockUser');
            MockLaboratorio = jasmine.createSpy('MockLaboratorio');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'Operatore': MockOperatore,
                'User': MockUser,
                'Laboratorio': MockLaboratorio
            };
            createController = function() {
                $injector.get('$controller')("OperatoreDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'italgaslabApp:operatoreUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
