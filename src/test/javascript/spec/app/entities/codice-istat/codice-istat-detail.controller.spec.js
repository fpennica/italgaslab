'use strict';

describe('Controller Tests', function() {

    describe('CodiceIstat Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockCodiceIstat;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockCodiceIstat = jasmine.createSpy('MockCodiceIstat');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'CodiceIstat': MockCodiceIstat
            };
            createController = function() {
                $injector.get('$controller')("CodiceIstatDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'italgaslabApp:codiceIstatUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
