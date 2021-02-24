'use strict';

describe('Controller Tests', function() {

    describe('Campione Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockCampione, MockCassetta, MockOperatore;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockCampione = jasmine.createSpy('MockCampione');
            MockCassetta = jasmine.createSpy('MockCassetta');
            MockOperatore = jasmine.createSpy('MockOperatore');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'Campione': MockCampione,
                'Cassetta': MockCassetta,
                'Operatore': MockOperatore
            };
            createController = function() {
                $injector.get('$controller')("CampioneDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'italgaslabApp:campioneUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
